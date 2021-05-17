import { useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { ArrowCircleLeftOutline as Back, DocumentAddOutline as AddResource, SaveOutline as Save } from '@graywolfai/react-heroicons'
import ReactPlayer from 'react-player'

import { Shell, Link, Separator, Input } from '../../components'

import { useMovie } from '../../hooks'

import Disney from './icons/disney_plus.png'
import Play from './icons/google_play.png'
import HBO from './icons/hbo.png'
import ITunes from './icons/itunes.png'
import Netflix from './icons/netflix.png'
import Prime from './icons/prime_video.png'
import Youtube from './icons/youtube.png'

const backdrop = movie => {
    const backdrop = movie?.resources?.find(res => res?.type === 'BACKDROP')?.url
    const poster = movie?.resources?.find(res => res?.type === 'POSTER')?.url

    return backdrop ? backdrop : poster
}
const poster = movie => movie?.resources?.find(res => res?.type === 'POSTER')?.url

export default function Profile() {
    const { id } = useParams()
    const { movie, update } = useMovie(id)

    const submit = async (event) => {
        await update({
            overview: movie.overview,
            resources: movie.resources
        })
    }

    return <Shell>
        <img style={{ height: '36rem' }}
            src={backdrop(movie)}
            alt={`${movie.title} backdrop`}
            className='absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' />

        <Link variant='primary'
            className='rounded-full absolute text-white top-4 left-8 flex items-center pl-2 pr-4 py-2 gap-4'
            to={`/movies/${id}`}
        >
            <Back className='w-8 h-8' />
            <span>Volver</span>
        </Link>

        <Link variant='primary'
            className='rounded-full absolute text-white top-4 right-8 flex items-center px-2 py-2 gap-4'
            to={`/movies/${id}`}
        >
            <Save
                className='w-8 h-8'
                onClick={submit}
            />
        </Link>

        <div className='mx-auto w-full max-w-screen-2xl p-8'>
            <Header movie={movie} />
            <Info movie={movie} />
            <View movie={movie} />
            <Cast movie={movie} />
        </div>
    </Shell>
}

function Header({ movie }) {
    return <header className='mt-64 relative flex items-end pb-8 mb-8'>
        <img style={{ aspectRatio: '2/3' }}
            src={poster(movie)}
            alt={`${movie.title} poster`}
            className='w-64 rounded-lg shadow-xl z-20' />
        <hgroup className='flex-1'>
            <h1 className={`bg-black bg-opacity-50 backdrop-filter backdrop-blur 
                                          text-right text-white text-6xl font-bold
                                          p-8`}>
                {movie.title}
            </h1>
            <Tagline movie={movie} />
        </hgroup>
    </header>
}
function Info({ movie }) {
    return <div className='grid grid-cols-5 gap-4'>
        <div className='col-span-4'>
            <h2 className='font-bold text-2xl text-white bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 p-4 shadow'>
                Argumento
            </h2>
            <textarea className='pt-8 p-4 w-full h-40' defaultValue={movie.overview} onChange={(event) => movie.overview = (event.target.value)} />
        </div>
        <div className='text-right'>
            <dl className='space-y-2'>
                <CrewMember movie={movie} job='Director' label='Dirección' />
                <CrewMember movie={movie} job='Producer' label='Producción' />
                <CrewMember movie={movie} job='Screenplay' label='Guión' />
                <CrewMember movie={movie} job='Original Music Composer' label='Banda sonora' />
            </dl>
        </div>
    </div>
}
function View({ movie }) {
    return <div className='flex gap-4 mt-8'>
        <div className='w-96 z-10'>
            <Links movie={movie} />
        </div>
        <div style={{
            aspectRatio: '16/9'
        }}
            className='flex-1 ml-8 mt-8 bg-pattern-2 flex items-center justify-center z-20'>
            <Trailer movie={movie} />
        </div>
    </div>
}
function Cast({ movie }) {
    return <>
        <h2 className='mt-16 font-bold text-2xl'>Reparto Principal</h2>
        <Separator />
        <ul className='w-full grid grid-cols-10 gap-2 overflow-hidden'>
            {
                movie?.cast?.slice(0, 10).map(person => <CastMember key={person.name} person={person} />)
            }
        </ul>
    </>
}
function Tagline({ movie }) {
    if (movie.tagline) {
        return <q className={`block text-3xl font-semibold text-black italic w-full px-8 py-4 text-right`}>
            {movie.tagline}
        </q>
    } else {
        return <span className='block text-3xl font-semibold py-4'>&nbsp;</span>
    }
}
function CrewMember({ movie, job, label }) {
    const people = movie?.crew?.filter(p => p.job === job)

    if (people?.length !== 0)
        return <div>
            <dt className='font-bold text-sm'>{label}</dt>
            {people?.map(p => <dd className='text-sm' key={`${job}/${p.id}`}>{p.name}</dd>)}
        </div>
    else return null
}
function Links({ movie }) {
    const [selection, setSelection] = useState('');
    const [url, setUrl] = useState('');
    const ref = useRef()
    const resources = movie?.resources?.filter(r => !['POSTER', 'BACKDROP', 'TRAILER'].includes(r.type))
    let links

    const addResource = async (event) => {
        if (selection !== '' && url !== '') {
            const newResource = {
                url: url,
                type: selection
            }
            movie.resources.push(newResource)
            resources.push(newResource)
            const children = [];
            children.push(<PlatformLink key={newResource.type} type={newResource.type} url={newResource.url} />);
            console.log(ref.current)
            /*const children = [];
            children.push(<PlatformLink key={newResource.type} type={newResource.type} url={newResource.url} />);
            console.log(children)

            ref.current.appendChild(children)*/
        }
    }

    if (resources?.length === 0) {
        links = <span className='block p-8 text-center bg-gray-300 font-bold'>
            No se han encontrado enlaces!
        </span>
    } else {
        links = <div>
            <div className='flex'>
                <select
                    className={`rounded font-bold text-xl bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 p-2 shadow text-white placeholder-white cursor-pointer`}
                    onChange={(event) => setSelection(event.target.value)}
                >
                    <option disabled selected>Fuente</option>
                    <option className='text-black'>Disney Plus</option>
                    <option className='text-black'>Google Play</option>
                    <option className='text-black'>HBO</option>
                    <option className='text-black'>iTunes</option>
                    <option className='text-black'>Netflix</option>
                    <option className='text-black'>Prime Video</option>
                    <option className='text-black'>YouTube</option>
                </select>
                <Input
                    className={`font-bold text-xl bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 p-4 shadow text-white ml-2 placeholder-white`}
                    placeholder='URL'
                    onChange={(event) => setUrl(event.target.value)}
                />
                <AddResource
                    className='w-12 h-12 cursor-pointer ml-3'
                    onClick={addResource}
                />
            </div>
            <div className='mt-6 ml-4'>
                <ul ref={ref} className='space-y-4'>
                    {
                        resources?.map(r => <PlatformLink key={r.type} type={r.type} url={r.url} />)
                    }
                </ul>
            </div>
        </div>
    }


    return <>
        <h2 className='font-bold text-2xl'>Ver ahora</h2>
        <Separator />
        { links}
    </>
}
function CastMember({ person }) {
    return <li className='overflow-hidden'>
        <img src={person?.picture}
            alt={`${person.name} profile`}
            className='w-full object-top object-cover rounded shadow'
            style={{ aspectRatio: '2/3' }} />
        <span className='font-bold block'> {person?.name} </span>
        <span className='text-sm block'> {person?.character} </span>
    </li>
}
function PlatformLink({ type = '', url = '', ...props }) {
    switch (type) {
        case 'DISNEY_PLUS':
            return <a target='_blank'
                rel='noreferrer'
                href={url}
                className={`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src={Disney} alt='Disney+ logo'
                    className='rounded-lg w-16 h-16'
                />
                <span className='font-bold'>
                    Reproducir en Disney Plus
                </span>
            </a>
        case 'GOOGLE_PLAY':
            return <a target='_blank'
                rel='noreferrer'
                href={url}
                className={`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src={Play} alt='Google Play logo'
                    className='rounded-lg w-16 h-16'
                />
                <span className='font-bold'>
                    Reproducir en Google Play
                </span>
            </a>
        case 'HBO':
            return <a target='_blank'
                rel='noreferrer'
                href={url}
                className={`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src={HBO} alt='HBO logo'
                    className='rounded-lg w-16 h-16'
                />
                <span className='font-bold'>
                    Reproducir en HBO
                </span>
            </a>
        case 'ITUNES':
            return <a target='_blank'
                rel='noreferrer'
                href={url}
                className={`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src={ITunes} alt='iTunes logo'
                    className='rounded-lg w-16 h-16'
                />
                <span className='font-bold'>
                    Reproducir en iTunes
                </span>
            </a>
        case 'NETFLIX':
            return <a target='_blank'
                rel='noreferrer'
                href={url}
                className={`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src={Netflix} alt='Netflix logo'
                    className='rounded-lg w-16 h-16'
                />
                <span className='font-bold'>
                    Reproducir en Netflix
                </span>
            </a>
        case 'PRIME_VIDEO':
            return <a target='_blank'
                rel='noreferrer'
                href={url}
                className={`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src={Prime} alt='Prime Video logo'
                    className='rounded-lg w-16 h-16'
                />
                <span className='font-bold'>
                    Reproducir en Prime Video
                </span>
            </a>
        case 'YOUTUBE':
            return <a target='_blank'
                rel='noreferrer'
                href={url}
                className={`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src={Youtube} alt='YouTube logo'
                    className='rounded-lg w-16 h-16'
                />
                <span className='font-bold'>
                    Reproducir en YouTube
                </span>
            </a>
        default: return null
    }
}
function Trailer({ movie, ...props }) {
    const trailer = movie?.resources?.find(r => r.type === 'TRAILER')

    if (trailer)
        return <ReactPlayer url={trailer.url} {...props} />
    else
        return <span className='text-white text-xl font-semibold p-8 backdrop-filter backdrop-blur bg-red-500 bg-opacity-30'>No se han encontrado trailers!</span>
}