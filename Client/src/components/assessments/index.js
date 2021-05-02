import React from 'react';
import { FilmOutline as Film } from '@graywolfai/react-heroicons'
import { useState, useRef } from 'react'
import { Button } from '../'
import Right from './icons/derecho.png'

export function Assessments({ comments, createComment, film }) {
    const [width, setWidth] = useState(0);
    const scroll = useRef(null)

    return <>
        {
            comments.content != null
            &&
            <div className='flex'>
                <div ref={scroll} className={`flex gap-8 overflow-hidden`}>
                    <ObtainComments comments={comments} />
                </div>
                <div className='h-96 cursor-pointer absolute right-0 w-16 bg-white opacity-80'></div>
                <button
                    className='h-96 absolute right-0 w-16'
                    onClick={() => {
                        scroll.current.scrollLeft += 400
                        setWidth(scroll.current.scrollLeft)
                    }}
                >
                    <img className='ml-3 h-10' src={Right} alt='' />
                </button>
                {
                    width > 0
                    &&
                    <>
                        <div className='h-96 cursor-pointer absolute w-16 bg-white opacity-80'></div>
                        <button
                            className='h-96 cursor-pointer absolute w-16'
                            onClick={() => {
                                scroll.current.scrollLeft -= 400
                                setWidth(scroll.current.scrollLeft)
                            }}
                        >
                            <img className='ml-3 h-10 transform rotate-180' src={Right} alt='' />
                        </button>
                    </>
                }
            </div>
        }
        <CreateComment createComment={createComment} film={film} />
    </>
}

function getRating(rating) {
    let children = []

    for (let i = 0; i < 10; i++) {
        if (i < rating) {
            children.push(<Film className={`inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 text-white`} />);
        } else {
            children.push(<Film className={`inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gray-300 text-white`} />);
        }
    }

    return children
}

function ObtainComments({ comments }) {
    let render = <></>

    render = comments.content.map((comment) =>
        <div key={comment.id} className='h-96 bg-white rounded p-4 flex flex-col shadow-md border-2' style={{ minWidth: '900px' }}>
            <div className='ml-8 mt-4 flex justify-between'>
                <span className='font-bold'>{comment.user.name}</span>
                <div className='text-right mr-10'>
                    {
                        getRating(comment.rating)
                    }
                </div>
            </div>
            <p className='p-10 md:overflow-hidden'>{comment.comment}</p>
        </div>
    );

    return render
}

function Ratings({ ratings, setRating }) {
    return <div className='mt-4'>
        {
            [...Array(10)].map((v, i) => (
                i < ratings
                    ?
                    <Film
                        className={`cursor-pointer inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 text-white`}
                        onClick={() => setRating(i + 1)}
                    />
                    :
                    <Film
                        className={`cursor-pointer inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gray-300 text-white`}
                        onClick={() => setRating(i + 1)}
                    />
            ))
        }
    </div>
}

function CreateComment({ createComment, film }) {
    const [rating, setRating] = useState(0);
    const [comment, setComment] = useState('');
    const textField = React.createRef()

    const submit = async (event) => {
        if (comment !== '') {
            console.log(textField.current.value)
            await createComment({
                user: localStorage.getItem('user'),
                film: film,
                comment: comment,
                rating: rating
            })
            setRating(0)
            textField.current.value = ''
            setComment('')
        }
    }

    return <div className='w-full h-64 mt-10 flex justify-start'>
        <div className='flex flex-col md:w-64'>
            <p className='font-bold'>Y a ti, ¿qué te ha parecido?</p>
            <Ratings ratings={rating} setRating={setRating} />
            <Button className='mt-32' type='submit' variant='primary' onClick={submit}>Publicar</Button>
        </div>
        <textarea
            name='search'
            type='text'
            placeholder='Escribe aquí tu comentario y comparte tu opinión con otros usuarios! Pero por favor, evita hacer spoilers...'
            className='md:p-4 ml-8 bg-white rounded placeholder-gray-400 font-medium border w-full'
            ref={textField}
            onChange={(event) => setComment(event.target.value)}
        />
    </div>
}